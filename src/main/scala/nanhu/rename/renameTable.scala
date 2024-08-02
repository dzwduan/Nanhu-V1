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

class RenameTable(implicit p: Parameter) extends CoreModule {
  val io = IO(new Bundle {
    val readPorts = Vec(3 * RenameWidth, new RatReadPort)
    val specWritePorts = Vec(CommitWidth, Input(new RatWritePort))
    val archWritePorts = Vec(CommitWidth, Input(new RatWritePort))
  })

  val rename_table_init = VecInit.tabulate(32)(i => 0.U(PhyRegIdxWidth.W))
  val spec_table = RegInit(rename_table_init)
  val arch_table = RegInit(rename_table_init)
// 用于实际作用，spec_table只用于最后的输出更新
  val spec_table_next = Wire(spec_table)
//TODO: why spec_table_next
  val t1_rdata = io.readPorts.map(r => RegNext(Mux(r.hold, r.data, spec_table_next(r.addr))))
  val t1_raddr = io.readPorts.map(r => RegEnable(r.addr, !r.hold))
  val t1_wspec = RegNext(io.specWritePorts)

  val t1_wspec_addr = t1_wspec.map(w => Mux(w.wen, UIntToOH(w.addr), 0.U))

  for((spec, i) <- spec_table_next.zipWithIndex) {
    val matchVec =t1_wspec_addr.map(w => w(i))
    val vMatch = ParallelPriorityMux(matchVec.reverse, t1_wspec.map(_.data).reverse)
    spec := Mux(VecInit(matchVec).asUInt.orR, vMatch, spec_table(i))
  }

  spec_table := spec_table_next

  // read , need bypass , 每个read需要与所有可能的write对比
  for ((read, i) <- io.readPorts.zipWithIndex) {
    val t0_bypass = io.specWritePorts.map(w => w.wen && Mux(read.hold, read.addr === w.addr, t1_raddr(i) === w.addr))
    val t1_bypass = RegNext(VecInit(t0_bypass))
    val bypass_data = ParallelPriorityMux(t1_bypass.reverse, t1_wspec.map(_.data).reverse)
    read.data := Mux(t1_bypass.asUInt.orR, bypass_data, t1_rdata(i))
  }

  for (arch <- io.archWritePorts) {
    when (arch.wen) {
      arch_table(arch.addr) := arch.data
    }
  }

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
  implicit val p = new Parameter
  ChiselStage.emitSystemVerilogFile(new RenameTable1)
}
