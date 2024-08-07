package nanhu.rename

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import nanhu._

class StdFreeList(size: Int)(implicit p: Parameter) extends BaseFreeList(size) {
  // freelist存放空闲的物理寄存器号，默认0-31已有映射，所以32开始
  val freelist = RegInit(VecInit(Seq.tabulate(size)(i => (i + 32).U((PhyRegIdxWidth + 1).W))))

  val headPtr     = RegInit(FreeListPtr(false, 0))
  val tailPtr     = WireInit(FreeListPtr(false, size - 1))
  val tailPtrNext = RegInit(FreeListPtr(true, 0)) //使用next进行更新
  val headPtrNext = WireInit(headPtr)

  val headVal = freelist(headPtr.value)
  val tailVal = freelist(tailPtr.value)

  val noWalk = !io.walk && !io.redirect

  //FIX: 一拍不止加一个

  // 分配空闲物理寄存器，要从freelist中拿出， head++, 需要考虑非空
  for (i <- 0 until RenameWidth) {
    val not_full    = !isFull(tailPtrNext, headPtr)
    val allocate_ok = io.allocateReq(i) && noWalk && io.canAllocate && io.doAllocate
    io.allocatePhyReg(i) := headVal
    val allocateCnt = PopCount(io.allocateReq)
    headPtrNext := Mux(allocate_ok, headPtr + allocateCnt, headPtr)
  }

  // 指令commit，加入freelist, tail++， 需要考虑非满, 条件不满足，不会进行
  for (i <- 0 until CommitWidth) {
    val not_empty = !isEmpty(tailPtrNext, headPtr)
    val free_ok   = io.freeReq(i) && noWalk && not_empty
    freelist(tailPtrNext.value) := io.freePhyReg(i)
    val freeCnt = PopCount(io.freeReq)
    tailPtrNext := Mux(free_ok, tailPtr + freeCnt, tailPtrNext)
  }

  // 需要检查是否能回退足够的大小
  val distance = distanceBetween(tailPtrNext, headPtr)
  val canBack  = io.walk && io.canAllocate
  when(canBack) {
    headPtrNext := headPtr - io.stepBack
  }

  headPtr := headPtrNext

  // 当前freelist中含有的空闲物理寄存器 > 分配的
  io.canAllocate := distance > io.stepBack
}

object StdFreeList extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new StdFreeList(p.NRPhyRegs))
}
