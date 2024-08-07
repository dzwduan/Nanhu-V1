package nanhu.rename


import chisel3._
import chisel3.util._
import nanhu._
import nanhu.rename._
import nanhu.utils._


class StdFreeList(size: Int)(implicit p: Parameter) extends BaseFreeList(size) {
    // freelist存放空闲的物理寄存器号，默认0-31已有映射，所以32开始
    val freelist = RegInit(VecInit(Seq.tabulate(size)(i => (i+32).U(PhyRegIdxWidth.W))))

    val headPtr = RegInit(FreeListPtr(false, 0))
    val tailPtr = WireInit(FreeListPtr(false, size-1))
    val tailPtrNext = RegInit(FreeListPtr(true, 0)) //使用next进行更新

    val noWalk = !io.walk && !io.redirect

    when (noWalk) {
      // 分配空闲物理寄存器，要从freelist中拿出， head++, 需要考虑非空
      for (i <- 0 until RenameWidth) {
        val allocate_ok = io.allocateReq(i) && !isEmpty(tailPtr, headPtr)
        io.allocatePhyReg(i) := Mux(allocate_ok, freelist(headPtr.value), io.allocatePhyReg(i))
        headPtr := headPtr + 1
      }

      // 指令commit，加入freelist, tail++， 需要考虑非满
      for (i <- 0 until CommitWidth) {
        val free_ok = io.freeReq(i) && !isFull(tailPtr, headPtr)
        freelist(tailPtrNext.value) := Mux(free_ok, io.freePhyReg(i), freelist(tailPtrNext.value))
        tailPtrNext := tailPtrNext + 1
      }
    }

    // 需要检查是否能回退足够的大小
    val distance = distanceBetween(tailPtrNext, headPtr)
    val canBack = io.walk && distance > io.stepBack
    when (canBack) {
      headPtr := headPtr - io.stepBack.asTypeOf(headPtr)
    }
}