package nanhu.rename

import chisel3._
import chisel3.util._
import nanhu._
import _root_.circt.stage.ChiselStage

class RatReadPort(implicit p: Parameter) extends CoreBundle {
  val hold = Input(Bool())
  val addr = Input(UInt(5.W))
  val data = Output(UInt(PhyRegIdxWidth.W))
}

class RatWritePort(implicit p: Parameter) extends CoreBundle {
  val wen = Bool()
  val addr = UInt(5.W)
  val data = UInt(PhyRegIdxWidth.W)
}

class RenameTable1(implicit p: Parameter) extends CoreModule {
  val io = IO(new Bundle {
    val readPorts = Vec(3 * RenameWidth, new RatReadPort)
    val specWritePorts = Vec(CommitWidth, Input(new RatWritePort))
    val archWritePorts = Vec(CommitWidth, Input(new RatWritePort))
  })

  val rename_table_init = VecInit.tabulate(32)(i => 0.U(PhyRegIdxWidth.W))
  val spec_table = RegInit(rename_table_init)
  val arch_table = RegInit(rename_table_init)

  // 只使用一个周期，不添加流水，禁用hold

  // read rename table, rename stage
  for (r <- io.readPorts) {
    r.data := spec_table(r.addr)
  }

  // write rename table, commit stage
  for (w <- io.specWritePorts) {
    when (w.wen) {
      spec_table(w.addr) := w.data
    }
  }

  for (arch <- io.archWritePorts) {
    when (arch.wen) {
      arch_table(arch.addr) := arch.data
    }
  }
}

object RenameTable1 extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new RenameTable1)
}