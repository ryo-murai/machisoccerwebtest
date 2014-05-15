package machisoccer

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

  val url = getUrl()

  val titleMonMatch = """\((\d+)/(\d+)\)""".r
  //val dateFormat = """\d\d\d\d/\d+/\d+"""
  //val eventDatesMatch = s"""月の開催予定は.*(${dateFormat}\(.*\)[,、]?\s?)+の(\d+)回""".r
  //val dateMatch = s"""(${dateFormat})""".r

  "The Machisoccer application form page" should "have consistent month and days for the events" in {
    openForm()

    val (year, month) = extractYearMonth(pageTitle)

    val today = DateTime.now
    year  should be >= today.getYear()
    month should be >= today.getMonthOfYear()
    month should be < 13
  }

  "The Machisoccer application form page" should "have proper description" in {
    openForm()

    val (year, month) = extractYearMonth(pageTitle)
    val description = getDescriptionText()

    description should include(s"${month}月の開催予定")

  }

  private def getUrl(): String = {
    // get url from system property unless it is unspecified
    new java.io.File("src/test/resources/form/index.htm").toURI().toASCIIString
  }

  private def openForm(): Unit = {
    go to url
    waiting until (titleContains("まちのサッカークラブ参加申し込み"))
  }

  private def extractYearMonth(title: String): (Int, Int) = {
    MachisoccerWeb.parseTitle(title) match {
      case (_, yearMonth) => (yearMonth.year.get, yearMonth.monthOfYear.get)
    }
  }

  private def getDescriptionText(): String = {
    xpath("/html/body/div/div/div[2]/div/div[1]").element.text
  }

}
