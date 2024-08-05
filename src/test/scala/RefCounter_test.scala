package nanhu

import chisel3._
import chisel3.util._
import nanhu.rename._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._


class RefCounter_test extends AnyFreeSpec with Matchers {
  "RefCounter_test pass" in {
    implicit val p = Parameter()
    val res = simulate(new RefCounter()) { dut =>
      
    }
  }