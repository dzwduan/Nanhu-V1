package nanhu

import chisel3._
import chisel3.util._
import nanhu.rename._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._

class StdFreeList_test extends AnyFreeSpec with Matchers {
  "StdFreeList_test pass" in {
    implicit val p = Parameter()
    val res = simulate(new StdFreeList(p.NRPhyRegs)) { dut =>

      reset()

      // test allocate
      set_walk(false)
      set_redirect(false)
      set_allocate(Array(false, false, true, true, true, false))
      // check_allocate(Array(32, 32, 32, 33, 34, 34))
      allocate_step()

      set_allocate(Array(true, false, true, false, true, false))
      allocate_step()

      set_allocate(Array(false, true, true, true, false, false))
      allocate_step()


      def set_walk(v : Boolean) : Unit = {
        dut.io.walk.poke(v.B)
      }

      def set_redirect(v : Boolean) : Unit = {
        dut.io.redirect.poke(v.B)
      }

      def allocate_step() : Unit = {
        dut.clock.step()
        dut.io.doAllocate.poke(false.B)
        for (i <- 0 until p.RenameWidth) {
          dut.io.allocateReq(i).poke(false.B)
        }
        dut.clock.step()
      }

      def set_allocate(v : Array[Boolean]) : Unit = {
        dut.io.doAllocate.poke(true.B)
        for (i <- 0 until p.RenameWidth) {
          dut.io.allocateReq(i).poke(v(i).B)
        }
      }

      def check_allocate(v : Array[Int]) : Unit = {
        for (i <- 0 until p.RenameWidth) {
          dut.io.allocatePhyReg(i).expect(v(i).U)
        }
      }



      def reset(): Unit = {
        dut.reset.poke(true.B)
        dut.clock.step()
        dut.reset.poke(false.B)
        dut.clock.step()
      }

    }
  }
}
