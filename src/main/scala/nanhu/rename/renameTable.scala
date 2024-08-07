package nanhu.rename

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import nanhu._


class RatReadPort(implicit p: Parameter) extends CoreBundle {
  //val hold = Input(Bool())
  val addr = Input(UInt(5.W))
  val data = Output(UInt(PhyRegIdxWidth.W))
}

class RatWritePort(implicit p: Parameter) extends CoreBundle {
  val wen = Bool()
  val addr = UInt(5.W)
  val data = UInt(PhyRegIdxWidth.W)
}

class RenameTable(implicit p: Parameter) extends CoreModule {
  val io = IO(new Bundle {
    val readPorts = Vec(3 * RenameWidth, new RatReadPort)
    val specWritePorts = Vec(CommitWidth, Input(new RatWritePort))
    val archWritePorts = Vec(CommitWidth, Input(new RatWritePort))
  })

  val rename_table_init = VecInit.tabulate(32)(i => 0.U(PhyRegIdxWidth.W))
  val spec_table = RegInit(rename_table_init)
  val arch_table = RegInit(rename_table_init)

  val spec_table_next = WireInit(spec_table)


  for (w <- io.archWritePorts) {
    when (w.wen) {
      spec_table(w.addr) := w.data
    }
  }

  for (r <- io.readPorts) {
    r.data := spec_table_next(r.addr)
  }

  for (w <- io.specWritePorts) {
    when (w.wen) {
      spec_table_next(w.addr) := w.data
    }
  }

  spec_table := spec_table_next
}


object RenameTable extends App {
  implicit val p: nanhu.Parameter = new Parameter
  ChiselStage.emitSystemVerilogFile(new RenameTable)
}
