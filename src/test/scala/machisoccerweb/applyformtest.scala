package machisoccer

import com.github.nscala_time.time.Imports._

import scala.util.Properties
import org.scalatest._
import org.scalatest.selenium._
import org.openqa.selenium._
import org.openqa.selenium.htmlunit._
import org.openqa.selenium.support.ui._
import org.openqa.selenium.support.ui.ExpectedConditions._

class MachisoccerFormSpec extends FlatSpec with ShouldMatchers with HtmlUnit {

  // initialize webDriver
  webDriver.setJavascriptEnabled(false)
  val waiting = new WebDriverWait(webDriver, 15)

  val url = getUrl()

  "The Machisoccer application form page" should "have proper title" in {
    openForm()

    val (year, month) = extractYearMonth(pageTitle)

    val today = DateTime.now
    year  should be >= today.getYear()
    month should be >= today.getMonthOfYear()
    month should be < 13
  }

  it should "have proper description" in {
    openForm()

    val desc = getDesc()

    desc.month should be < 13
    withClue(s"consistency between events and number:") {
      desc.eventDates.length should be (desc.numEvents)
    }
    withClue("shinnendo description :") {
      if(desc.shinnendo) desc.month should be < 6 
      else desc.month should be > 5
    }

    desc.eventDates.foreach { d =>
      withClue(s"month of event date for $d :") {
        d.monthOfYear.get should be (desc.month)
      }

      desc.dueDate should be < d
    }
  }

  it should "be consistent in title and description" in {
    openForm()

    val (year, month) = extractYearMonth(pageTitle)
    val desc = getDesc()
    withClue("month written in title and description: ") {
      month should be (desc.month)
    }

    desc.eventDates.foreach { d =>
      withClue(s"year of event date for $d :") {
        d.year.get should be (year)
      }
      withClue(s"month of event date for $d :") {
        d.monthOfYear.get should be (month)
      }
    }
  }

  it should "have event attend items stated in the description" in {
    openForm()
    val desc = getDesc()
    val eventItems = formItemLabels().toSeq
                      .filter{_.startsWith("20")}
                      .map{_.replace("*", "").trim}

    withClue("compare num events in form item and description") {
      desc.numEvents should be (eventItems.length)
    }

    val formItemDates = eventItems.map(MachisoccerWeb.parseDateEx(_))
    withClue("compare description and form items") {
      desc.eventDates should equal (formItemDates)
    }
  }

  private def getUrl(): String = {
    // get url from system property unless it is unspecified
    //"http://goo.gl/RE5QRW"
    Properties.envOrElse("TARGET_URL", localDebugFormUrl)
  }

  private lazy val localDebugFormUrl: String = 
    new java.io.File("src/test/resources/form/index.htm").toURI().toASCIIString

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

  private def getDesc(): Descr= {
    MachisoccerWeb.parseDesc(getDescriptionText())
  }

  private def formItemLabels() = {
    xpath("""//*[@id='ss-form']/ol/div/div/div/label/div[1]""").findAllElements.map(_.text)
  }
}
