// package nanhu

// import chisel3._
// import chisel3.util._
// import nanhu.rename._
// import org.scalatest.freespec.AnyFreeSpec
// import org.scalatest.matchers.must.Matchers
// import simulation.Simulator._


// class RefCounter_test extends AnyFreeSpec with Matchers {
//   "RefCounter_test pass" in {
//     implicit val p = Parameter()
//     val res = simulate(new RefCounter()) { dut =>

//       val refCounter = Array.tabulate(p.NRPhyRegs)(i => if (i < p.NRArchRegs) 1 else 0)

//       reset()
//       check()

//       allocate(0, 1)
//       allocate(1, 2)
//       step_check()
//       allocate(0, 2)
//       allocate(1, 3)
//       step_check()
//       allocate(0, 4)
//       allocate(1, 5)
//       step_check()
//       free(0, 1)
//       free(1, 2)
//       step_check()
//       free(0, 2)
//       free(1, 3)
//       step_check()
//       free(0, 4)
//       free(1, 5)
//       step_check()




//       def clear(): Unit = {
//         for (i <- 0 until p.NRPhyRegs) {
//           dut.io.allocate(i).valid.poke(false.B)
//           dut.io.deallocate(i).valid.poke(false.B)
//         }
//       }

//       def reset(): Unit = {
//         dut.reset.poke(true.B)
//         dut.clock.step()
//         dut.reset.poke(false.B)
//         for (i <- 0 until p.NRPhyRegs+1) {
//           if (i < p.NRArchRegs) {
//             refCounter(i) = 1
//           } else {
//             refCounter(i) = 0
//           }
//         }
//       }

//       def step_check() : Unit = {
//         dut.clock.step()
//         check()
//         clear()
//       }

//       def allocate(port : Int, data : Int) : Unit = {
//         assert(port < p.RenameWidth)
//         dut.io.allocate(port).valid.poke(true.B)
//         dut.io.allocate(port).bits.poke(data.U)
//       }

//       def free(port : Int, data : Int) : Unit = {
//         assert(port < p.CommitWidth)
//         dut.io.deallocate(port).valid.poke(true.B)
//         dut.io.deallocate(port).bits.poke(data.U)
//       }


//       def check() : Unit = {
//         // if allocate valid , ++
//         dut.io.allocate.foreach( v =>
//           if (v.valid.peekValue.asBigInt != 0) {
//             val idx = v.bits.peekValue.asBigInt.toInt
//             refCounter(idx) = refCounter(idx) + 1
//           }
//         )

//         // if free valid, --
//         dut.io.deallocate.foreach(v =>
//           if (v.valid.peekValue.asBigInt != 0) {
//             val idx = v.bits.peekValue.asBigInt.toInt
//             refCounter(idx) = refCounter(idx) - 1
//             assert(refCounter(idx) >= 0)
//           }
//         )

//         val freeValid = Array.fill(p.CommitWidth)(false)
//         val freeReg = Array.fill(p.CommitWidth)(0)

//         dut.io.deallocate.zipWithIndex.foreach { case (v, i) =>
//           if (v.valid.peekValue().asBigInt != 0) {
//             freeValid(i) = refCounter(i) == 0
//             freeReg(i) = v.bits.peekValue().asBigInt.toInt
//           }
//         }

//         // check duplicate
//         for (i <- 0 until p.CommitWidth) {
//           if (freeValid(i)) {
//             for (j <- 0 until i) {
//               if (freeValid(j) && freeReg(i) == freeReg(j)) {
//                 freeValid(i) = false
//               }
//             }
//           }
//         }

//         val outVaid = freeValid.reduce(_ | _) && (dut.io.deallocate.map(_.valid).reduce(_ | _).peekValue().asBigInt != 0)
//         dut.io.freeRegs.zipWithIndex.foreach{ case (v, i) =>
//           v.valid.expect(freeValid(i).B, s" out port $i valid error")
//           if (freeValid(i)) v.bits.expect(freeReg(i).U, s"out port $i bits error")
//         }

//       }









      
//     }
//   }
// }