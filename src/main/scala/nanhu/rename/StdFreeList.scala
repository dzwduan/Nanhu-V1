package nanhu.rename

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import nanhu._
import nanhu.utils._

class StdFreeList(size: Int)(implicit p: Parameter) extends BaseFreeList(size) {
  // freelist存放空闲的物理寄存器号，默认0-31已有映射，所以32开始
  val freelist = RegInit(VecInit(Seq.tabulate(size)(i => (i + 32).U((PhyRegIdxWidth).W))))

  val headPtr     = RegInit(FreeListPtr(false, 0))
  val tailPtr     = RegInit(FreeListPtr(false, size - 1))
  val headPtrNext = WireInit(headPtr)
  val tailPtrNext = WireInit(tailPtr)

  val noDirOrWalk = !io.walk && !io.redirect
  //Fix ppa
  io.canAllocate := RenameWidth.U <= distanceBetween(tailPtr, headPtr)

  headPtrNext := headPtr + PopCount(io.allocateReq)
  tailPtrNext := tailPtr + PopCount(io.freeReq)
  headPtr     := headPtrNext
  tailPtr     := tailPtrNext

  val headOH = UIntToOH(headPtr.value)
  // 这里使用CircularShift因为case所以不需要new，并没有使用object
  val headOHCir = CircularShift(headOH)
  // 将每一拍对应的每一个allocaeReq转化为对应的OneHot，循环左移类似于++
  val headOHCirVec = VecInit(Seq.tabulate(RenameWidth + 1)(headOHCir.left))

  val allocCandidates = VecInit(headOHCirVec.map(h => Mux1H(h, freelist)))

  for (i <- 0 until RenameWidth) {
    io.allocatePhyReg(i) := allocCandidates(PopCount(io.allocateReq.take(i).map(a => a === true.B)))
  }

  for (i <- 0 until CommitWidth) {
    when (io.freeReq(i)) {
      freelist(tailPtr.value) := io.freePhyReg(i)
    }
  }
  
  


  val enableCheck = true

  if (enableCheck) {
    val ref = Module(new StdFreeListCheck(size))
    // io.allocatePhyReg := DontCare
    // io.canAllocate := DontCare
    dontTouch(ref.io.canAllocate)
    dontTouch(ref.io.allocatePhyReg)
    ref.io.freeReq     := io.freeReq
    ref.io.doAllocate  := io.doAllocate
    ref.io.allocateReq := io.allocateReq
    ref.io.redirect    := io.redirect
    ref.io.walk        := io.walk
    ref.io.freePhyReg  := io.freePhyReg
    ref.io.stepBack    := io.stepBack

    // assert(ref.io.canAllocate === io.canAllocate, "canAllocate failed") 
    // assert(ref.io.allocatePhyReg === io.allocatePhyReg, "allocatePhyReg failed")
  }
}







class StdFreeListCheck(size: Int)(implicit p: Parameter) extends BaseFreeList(size) {

  val freeList = RegInit(VecInit(Seq.tabulate(size)(i => (i + 32).U(PhyRegIdxWidth.W))))
  val headPtr  = RegInit(FreeListPtr(false, 0))

  val headPtrOH      = RegInit(1.U(size.W))
  val headPtrOHShift = CircularShift(headPtrOH)
  // may shift [0, RenameWidth] steps , 每次allocation headptr都会左移
  val headPtrOHVec = VecInit.tabulate(RenameWidth + 1)(headPtrOHShift.left)

//======================================= free ======================================================//
  //
  // free committed instructions' `old_pdest` reg
  //
  val lastTailPtr = RegInit(FreeListPtr(true, 0)) // tailPtr in the last cycle (need to add freeReqReg)
  val tailPtr     = Wire(new FreeListPtr) // this is the real tailPtr

  val freeReqReg = RegNext(io.freeReq)
  for (i <- 0 until CommitWidth) {
    val offset = if (i == 0) 0.U else PopCount(freeReqReg.take(i))
    val enqPtr = lastTailPtr + offset

    // Why RegNext: for better timing
    // Why we can RegNext: these free registers won't be used in the next cycle,
    // since we set canAllocate only when the current free regs > RenameWidth.
    when(freeReqReg(i)) {
      freeList(enqPtr.value) := RegNext(io.freePhyReg(i))
    }
  }

  tailPtr     := lastTailPtr + PopCount(freeReqReg)
  lastTailPtr := tailPtr

//=======================================end free ===================================================//

//===================================== allocation ==================================================//

  //
  // allocate new physical registers for instructions at rename stage
  //
  val freeRegCnt = Wire(UInt()) // number of free registers in free list
  io.canAllocate := RegNext(freeRegCnt >= RenameWidth.U) // use RegNext for better timing

  //分配就是出队，from head ptr
  val phyRegCandidates = VecInit(headPtrOHVec.map(sel => Mux1H(sel, freeList)))

  for (i <- 0 until RenameWidth) {
    io.allocatePhyReg(i) := phyRegCandidates( /* if (i == 0) 0.U else */ PopCount(io.allocateReq.take(i)))
  }
  val numAllocate     = PopCount(io.allocateReq)
  val headPtrAllocate = headPtr + numAllocate
  val headPtrNext     = Mux(io.canAllocate && io.doAllocate, headPtrAllocate, headPtr)
  freeRegCnt := distanceBetween(tailPtr, headPtrNext)

//==============================================================================================//

  // priority: (1) exception and flushPipe; (2) walking; (3) mis-prediction; (4) normal dequeue
  val realDoAllocate = !io.redirect && io.canAllocate && io.doAllocate
  headPtr := Mux(io.walk, headPtr - io.stepBack, Mux(realDoAllocate, headPtrAllocate, headPtr))

  // Since the update of headPtr should have a good timing,
  // we calculate the OH index here to optimize the freelist read timing.
  // may shift [0, RenameWidth] steps
  val stepBackHeadPtrOHVec = VecInit.tabulate(CommitWidth + 1)(headPtrOHShift.right)
  val stepBackHeadPtrOH    = stepBackHeadPtrOHVec(io.stepBack)
  headPtrOH := Mux(io.walk, stepBackHeadPtrOH, Mux(realDoAllocate, headPtrOHVec(numAllocate), headPtrOH))

}

object StdFreeList extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new StdFreeList(p.NRPhyRegs))
}
