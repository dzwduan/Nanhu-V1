package nanhu

import chisel3._
import chisel3.util._


case class Parameter (
  XLEN: Int = 64,
  DecodeWidth: Int = 6,
  RenameWidth: Int = 6,
  CommitWidth: Int = 6,

  NRArchRegs: Int = 32,
  NRPhyRegs:   Int = 128,
  RobSize : Int = 192,
)


trait HasNanhuParameters {
  implicit val p : Parameter

  val XLEN = p.XLEN
  val NRArchRegs = p.NRArchRegs
  val NRPhyRegs = p.NRPhyRegs
  val DecodeWidth = p.DecodeWidth
  val RenameWidth = p.RenameWidth
  val CommitWidth = p.CommitWidth

  val RobSize = p.RobSize
  val PhyRegIdxWidth = log2Ceil(p.NRPhyRegs)
}

abstract class CoreBundle(implicit val p : Parameter) extends Bundle with HasNanhuParameters
abstract class CoreModule(implicit val p : Parameter) extends Module with HasNanhuParameters
