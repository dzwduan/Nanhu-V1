package nanhu.rename

import chisel3._
import chisel3.util._
import nanhu._
import _root_.circt.stage.ChiselStage

// 维护preg的引用计数
class RefCounter(implicit p : Parameter) extends CoreModule {
  val io = IO(new Bundle {
    val allocate =  Vec(RenameWidth, Flipped(ValidIO(UInt(PhyRegIdxWidth.W))))  // rename
    val deallocate = Vec(CommitWidth, Flipped(ValidIO(UInt(PhyRegIdxWidth.W)))) // commit
    val freeRegs = Vec(CommitWidth, ValidIO(UInt(PhyRegIdxWidth.W))) // 返回释放的物理寄存器号
  })

  val refcnt = RegInit(VecInit.fill(p.NRPhyRegs)(0.U(PhyRegIdxWidth.W)))

  val refcntInc = WireInit(refcnt)
  val refcntDec = WireInit(refcnt)
  val refcntNext = WireInit(refcnt)

  val allocate = RegNext(io.allocate)
  val deallocate =  RegNext(io.deallocate)
  val freeregs = io.freeRegs

  for ( i <- 0 until CommitWidth) {
  
    // 从非零值变为零值
    val isNonZero = deallocate(i).valid && refcnt(deallocate(i).bits) =/= 0.U
    val canFree = refcntDec(deallocate(i).bits) === (refcnt(deallocate(i).bits) + refcntInc(deallocate(i).bits))

    // 不重复释放
    val hasDup = deallocate.take(i).map(d => deallocate(i).valid && deallocate(i).bits === d.bits)
    val dupBlock = if (i == 0) false.B else VecInit(hasDup).asUInt.orR
    freeregs(i).valid := RegNext(isNonZero && !dupBlock) && RegNext(canFree)
    freeregs(i).bits := RegNext(deallocate(i).bits)
  }

  for (i <- 0 until p.NRPhyRegs) {
    refcntInc(i) := PopCount(allocate.map(a => a.valid && a.bits === i.U))
    refcntDec(i) := PopCount(deallocate.map(d => d.valid && d.bits === i.U))
    refcntNext(i) := refcnt(i) + refcntInc(i) - refcntDec(i)
    refcnt(i) := refcntNext(i)
  }
  
}


object RefCounter extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new RefCounter)
}


