package nanhu.rename

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import nanhu._
import nanhu.utils._

class StdFreeList(size: Int)(implicit p: Parameter) extends BaseFreeList(size){
  // freelist存放空闲的物理寄存器号，默认0-31已有映射，所以32开始
  val freelist = RegInit(VecInit(Seq.tabulate(size)(i => (i + 32).U((PhyRegIdxWidth).W))))

  val headPtr     = RegInit(FreeListPtr(false, 0))
  val tailPtr     = RegInit(FreeListPtr(false, size - 1))
  val headPtrNext = WireInit(headPtr)
  val tailPtrNext = WireInit(tailPtr)

  val noDirOrWalk    = !io.walk && !io.redirect
  io.canAllocate := PopCount(io.allocateReq) <= distanceBetween(tailPtr, headPtr)

  headPtrNext := headPtr + PopCount(io.allocateReq)
  tailPtrNext := tailPtr + PopCount(io.freeReq)
  headPtr := headPtrNext
  tailPtr := tailPtrNext

  val headOH = UIntToOH(headPtr.value)
  // 这里使用CircularShift因为case所以不需要new，并没有使用object
  val headOHCir = CircularShift(headOH)
  // 将每一拍对应的每一个allocaeReq转化为对应的OneHot，循环左移类似于++
  val headOHCirVec = VecInit(Seq.tabulate(RenameWidth+1)(headOHCir.left))

  val allocCandidates = VecInit(headOHCirVec.map(h => Mux1H(h, freelist)))

  for (i <- 0 until RenameWidth) {
    io.allocatePhyReg(i) := allocCandidates(PopCount(io.allocateReq.take(i).map(a => a === true.B)))
  }


}

object StdFreeList extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new StdFreeList(p.NRPhyRegs))
}
