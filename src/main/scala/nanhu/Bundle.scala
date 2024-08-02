package nanhu

import chisel3._
import chisel3.util._

class RobCommitIO(implicit p : Parameter) extends CoreBundle{
  val isCommit    = Output(Bool())
  val commitValid = Vec(CommitWidth, Output(Bool()))
  val isWalk      = Output(Bool())

  val walkValid = Vec(CommitWidth, Output(Bool()))
  val info      = Vec(CommitWidth, Output(new RobCommitInfo))
}

class RobCommitInfo(implicit p : Parameter) extends RobEntryData {
    // these should be optimized for synthesis verilog
    //   val pc = UInt(VAddrBits.W)

  def connectEntryData(data: RobEntryData) = {
    ldest := data.ldest
    rfWen := data.rfWen
    // fpWen := data.fpWen
    wflags := data.wflags
    // wvcsr := data.wvcsr
    vecWen := data.vecWen
    // commitType := data.commitType
    pdest := data.pdest
    old_pdest := data.old_pdest
    // ftqIdx := data.ftqIdx
    // ftqOffset := data.ftqOffset
    // vtypeWb := data.vtypeWb
    // isVector := data.isVector
    isOrder := data.isOrder
  }
}

class RobEntryData(implicit p : Parameter) extends CoreBundle {
  val ldest = UInt(5.W)
  val rfWen = Bool()
  val fpWen = Bool()
  val vecWen = Bool()
  val wflags = Bool()
//   val wvcsr = Bool()
//   val commitType = CommitType()
  val pdest     = UInt(PhyRegIdxWidth.W)
  val old_pdest = UInt(PhyRegIdxWidth.W)
//   val ftqIdx = new FtqPtr
//   val ftqOffset = UInt(log2Up(PredictWidth).W)
//   val vtypeWb  = Bool()
//   val isVector = Bool()
  val isOrder  = Bool()
}

class FusionDecodeInfo extends Bundle {
  val rs2FromRs1 = Output(Bool())
  val rs2FromRs2 = Output(Bool())
  val rs2FromZero = Output(Bool())
}

class Redirect(implicit p: Parameter) extends CoreBundle {
//   val robIdx = new RobPtr

//   def flushItself() = RedirectLevel.flushItself(level)
}

class CfCtrl(implicit p: Parameter) extends CoreBundle {
 val ctrl = new CtrlSignals
}

class CtrlSignals(implicit p: Parameter) extends CoreBundle {
  val rfWen = Bool()
  val fpWen = Bool()
  val lsrc = Vec(3, UInt(5.W))
  val ldest = UInt(5.W)
}

class MicroOp(implicit p : Parameter) extends CfCtrl {
    // val srcState = Vec(3, SrcState())
  val psrc = Vec(3, UInt(PhyRegIdxWidth.W))
  val pdest = UInt(PhyRegIdxWidth.W)
  val old_pdest = UInt(PhyRegIdxWidth.W)
  // val robIdx = new RobPtr
  // val lqIdx = new LqPtr
  // val sqIdx = new SqPtr
  // val eliminatedMove = Bool()
  // val debugInfo = new PerfDebugInfo
}