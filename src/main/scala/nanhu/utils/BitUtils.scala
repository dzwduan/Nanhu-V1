package nanhu.utils

import chisel3._
import chisel3.util._

import scala.math.min

class CircularShift(data: UInt) {
  private def helper(step: Int, isLeft: Boolean): UInt = {
    if (step == 0) {
      data
    }
    else {
      val splitIndex = if (isLeft) {
        data.getWidth - (step % data.getWidth)
      } else {
        step % data.getWidth
      }
      Cat(data(splitIndex - 1, 0), data(data.getWidth - 1, splitIndex))
    }
  }
  def left(step: Int): UInt = helper(step, true)
  def right(step: Int): UInt = helper(step, false)
}