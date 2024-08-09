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

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      val randBool = scala.util.Random.nextBoolean
      val randInt = scala.util.Random.nextInt(p.NRPhyRegs)

      // check init
      dut.io.canAllocate.expect(true.B)
      dut.io.allocatePhyReg.foreach(_.expect(32.U))
      dut.io.freePhyReg.foreach(_.expect(0.U))

      // random check allocate n times
      for ( i <- 0 until randInt) {
        set_walk(false)
        set_redirect(false)
        set_allocate(Array.fill(p.RenameWidth)(randBool))
        allocate_step()
      }


      // check free n times
      for (i <- 0 until randInt) {
        set_free(Array.fill(p.CommitWidth)(randBool))
        set_free_vals(Array.fill(p.CommitWidth)(randInt))
        free_step()
      }

      // check stepBack n times
      for (i <- 0 until randInt) {
        set_back(randInt)
      }

      // both check allocate free stepBack
      for (i <- 0 until randInt) {
        set_walk(false)
        set_redirect(false)
        set_allocate(Array.fill(p.RenameWidth)(randBool))
        allocate_step()

        set_free(Array.fill(p.CommitWidth)(randBool))
        set_free_vals(Array.fill(p.CommitWidth)(randInt))
        free_step()

        set_back(randInt)
      }


      def set_walk(v : Boolean) : Unit = {
        dut.io.walk.poke(v.B)
      }

      def set_redirect(v : Boolean) : Unit = {
        dut.io.redirect.poke(v.B)
      }

      def set_back(v : Int) : Unit = {
        dut.io.stepBack.poke(v.U)
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

      // def check_allocate(v : Array[Int]) : Unit = {
      //   for (i <- 0 until p.RenameWidth) {
      //     dut.io.allocatePhyReg(i).expect(v(i).U)
      //   }
      // }

      //不考虑do_allocate
      def set_free(v : Array[Boolean]) : Unit = {
        for (i <- 0 until p.CommitWidth) {
          dut.io.freeReq(i).poke(v(i).B)
        }
      }

      def set_free_vals(v : Array[Int]) : Unit = {
        for (i <- 0 until p.CommitWidth) {
          dut.io.freePhyReg(i).poke(v(i).U)
        }
      }

      def free_step() : Unit = {
        dut.clock.step()
        for (i <- 0 until p.CommitWidth) {
          dut.io.freeReq(i).poke(false.B)
          dut.io.freePhyReg(i).poke(0.U)
        }
        dut.clock.step()
      }




    }
  }
}
