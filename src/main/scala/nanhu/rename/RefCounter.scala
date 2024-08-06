package nanhu.rename

import chisel3._
import chisel3.util._
import nanhu._
import _root_.circt.stage.ChiselStage


class RefCounter(implicit p : Parameter) extends CoreModule {
  val io = IO(new Bundle {
    val allocate =  Vec(RenameWidth, Flipped(ValidIO(UInt(PhyRegIdxWidth.W))))
    val deallocate = Vec(CommitWidth, Flipped(ValidIO(UInt(PhyRegIdxWidth.W))))
    val freeRegs = Vec(CommitWidth, ValidIO(UInt(PhyRegIdxWidth.W)))
  })

  val refcnt = RegInit(VecInit.fill(32)(0.U(PhyRegIdxWidth.W)))

  val refcntInc = Wire(refcnt)
  val refcntDec = Wire(refcnt)
  val refcntNext = Wire(refcnt)

  val allocate = io.allocate
  val deallocate = io.deallocate

  // size 默认
  val allocateOH = allocate.map(i => UIntToOH(i.bits, p.NRPhyRegs))
  val deallocateOH = deallocate.map(i => UIntToOH(i.bits, p.NRPhyRegs))

  // update refcnt , 不考虑0号物理寄存器
  for (i <- 1 until p.NRPhyRegs) {
    // 检测第i项是否存在valid && allocate, 只有0/1
    refcntInc(i) := PopCount(allocate.zip(allocateOH).map(a => a._1.valid && a._2(i)))
    refcntDec(i) := PopCount(deallocate.zip(deallocateOH).map(a => a._1.valid && a._2(i)))
    refcntNext(i) := refcnt(i) + refcntInc(i) - refcntDec(i)
    refcnt(i) := refcntNext(i)
  }

  // update freeRegs
  // valid需要考虑 重复释放 以及 引用计数需要从非零变为0的过程
  for ((deallo, i) <- deallocate.zipWithIndex) {
    io.freeRegs(i).bits := deallo.bits

    val isNoneZero = deallo.valid && deallo.bits  =/= 0.U
    //TODO: i need if i = 3 , check deallo.bits =?= deallocate(0/1/2).bits
    val hasDup = deallocate.take(i).map{de => de.valid && (de.bits === deallocate(i).bits)}
    val dupBlock = if (i==0) false.B else VecInit(hasDup).asUInt.orR
    io.freeRegs(i).valid := RegNext(isNoneZero && !dupBlock)
  }
}


object RenameTable1 extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new RefCounter)
}


