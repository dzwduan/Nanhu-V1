package nanhu.utils
import chisel3._
import chisel3.layer.{Convention, Layer, block}


object Verification extends Layer(Convention.Bind) {
  object Assert extends Layer(Convention.Bind)
  object Debug extends Layer(Convention.Bind)
}