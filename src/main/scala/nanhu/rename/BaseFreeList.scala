package nanhu.rename

import chisel3._
import chisel3.util._
import nanhu._
import nanhu.utils._

abstract class BaseFreeList(size: Int)(implicit p: Parameter) extends CoreModule with HasCircularQueuePtrHelper {
  val io = IO(new Bundle {
    val redirect = Input(Bool())
    val walk     = Input(Bool())

    val allocateReq    = Input(Vec(RenameWidth, Bool())) //该路分配请求是否有效
    val allocatePhyReg = Output(Vec(RenameWidth, UInt(PhyRegIdxWidth.W)))
    val canAllocate    = Output(Bool()) //是否能够分配物理寄存器
    val doAllocate     = Input(Bool())

    val freeReq    = Input(Vec(CommitWidth, Bool())) //是否请求释放物理寄存器
    val freePhyReg = Input(Vec(CommitWidth, UInt(PhyRegIdxWidth.W)))

    val stepBack = Input(UInt(log2Up(CommitWidth + 1).W)) // 失败回滚步长
  })

  class FreeListPtr extends CircularQueuePtr[FreeListPtr](size)

  object FreeListPtr {
    def apply(f: Boolean, v: Int): FreeListPtr = {
      val ptr = Wire(new FreeListPtr)
      ptr.flag  := f.B
      ptr.value := v.U
      ptr
    }
  }
}
