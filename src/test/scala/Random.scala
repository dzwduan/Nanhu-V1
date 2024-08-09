package nanhu.test
import scala.language.postfixOps
import scala.util.Random

object RandomNum {
  def randomNewInt(n: Int): List[Int] = {
    var arr = 0 to 20 toArray
    var outList: List[Int] = Nil
    var border = 6 //随机数范围
    for (i <- 0 to n - 1) { //生成n个数
      val index = (new Random).nextInt(border)
      outList    = outList ::: List(arr(index))
      arr(index) = arr.last //将最后一个元素换到刚取走的位置
      arr        = arr.dropRight(1) //去除最后一个元素
      border -= 1
    }
    outList
  }

  def randomNewBool(n: Int): List[Boolean] = {
    var outList: List[Boolean] = Nil
    for (i <- 0 to n - 1) {
      outList = outList ::: List((new Random).nextBoolean())
    }
    outList
  }
}
