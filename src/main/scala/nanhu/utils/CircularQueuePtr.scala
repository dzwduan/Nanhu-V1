package nanhu.utils

import chisel3._
import chisel3.util._
import nanhu._


class CircularQueuePtr[T <: CircularQueuePtr[T]](val entries : Int) extends Bundle {
  val cq_width = log2Up(entries)
  val value = UInt(cq_width.W)
  val flag = Bool()

  def +(v : UInt) : T = {
    val ret = WireInit(this.asInstanceOf[T])
    if (isPow2(entries)) {
        ret := (Cat(flag, value) + Cat(0.U(1.W), v)).asTypeOf(ret)
    } else {
        val tmp = Cat(0.U(1.W), value) + v
        val reverse = tmp >= entries.U
        ret.flag := flag ^ reverse
        ret.value := Mux(reverse, tmp - entries.U, tmp)
    }
    ret
  }

  def -(v : UInt) : T = {
    val flipped_new_ptr = this + (this.entries.U - v)
    val new_ptr = Wire(this.asInstanceOf[T].cloneType)
    new_ptr.flag := !flipped_new_ptr.flag
    new_ptr.value := flipped_new_ptr.value
    new_ptr
  }

  def ===(that : T) : Bool = {
    this.asUInt === that.asUInt
  }

  def =/=(that : T) : Bool = {
    this.asUInt =/= that.asUInt
  }

  def toOH : UInt = UIntToOH(value, entries)
}

trait HasCircularQueuePtrHelper {
  def isEmpty[T <: CircularQueuePtr[T]](enq_ptr : T, deq_ptr : T) : Bool = {
    enq_ptr === deq_ptr
  }

  def isFull[T <: CircularQueuePtr[T]](enq_ptr : T, deq_ptr : T) : Bool = {
    enq_ptr.flag =/= deq_ptr.flag && enq_ptr === deq_ptr
  }

  def distanceBetween[T <: CircularQueuePtr[T]](enq_ptr: T, deq_ptr: T): UInt = {
    assert(enq_ptr.entries == deq_ptr.entries)
    Mux(enq_ptr.flag === deq_ptr.flag,
      enq_ptr.value - deq_ptr.value,
      enq_ptr.entries.U + enq_ptr.value - deq_ptr.value)
  }
}