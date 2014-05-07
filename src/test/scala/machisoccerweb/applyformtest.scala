import com.github.nscala_time.time.Imports._

import org.scalatest._
import org.scalatest.selenium._
import org.openqa.selenium._
import org.openqa.selenium.htmlunit._
import org.openqa.selenium.support.ui._
import org.openqa.selenium.support.ui.ExpectedConditions._

class MachisoccerFormSpec extends FlatSpec with ShouldMatchers with HtmlUnit {
  //implicit val webDriver: WebDriver = new HtmlUnitDriver

  // initialize webDriver
  webDriver.setJavascriptEnabled(false)
  val waiting = new WebDriverWait(webDriver, 15)

  val host = new java.io.File("src/test/resources/form/index.htm").toURI().toASCIIString

  val titleMonMatch = """\((\d+)/(\d+)\)""".r

  "The Machisoccer application form page" should "have consistent month and days for the events" in {
    go to host
    waiting until (titleContains("まちのサッカークラブ参加申し込み"))

    val (year, month) = extractYearMonth(pageTitle)

    val today = DateTime.now
    year  should be >= today.getYear()
    month should be >= today.getMonthOfYear()
    month should be < 13
  }

  private def extractYearMonth(title: String): (Int, Int) = {
    val p = for {
      titleMonMatch(y, m) <- titleMonMatch findFirstIn title
    } yield (y.toInt, m.toInt)

    p.get
  }
}
