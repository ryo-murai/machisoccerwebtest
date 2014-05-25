package machisoccer

import com.github.nscala_time.time.Imports._
//import org.joda.time._

import scala.util.parsing.combinator._

object MachisoccerWeb extends RegexParsers {

  // -----------------------------------------------
  //  parsers for generic use
  // -----------------------------------------------
  def sep: Parser[String] = """[,、]""".r
  def rest: Parser[String] = """.*""".r
  def openp: Parser[String] = "(" | "（"
  def closep: Parser[String] = ")" | "）"
  def slash: Parser[String] = "/" | "／"

  // -----------------------------------------------
  //  parsers for date
  // -----------------------------------------------
  val dayOfWeekLabels = IndexedSeq("月","火","水","木","金","土","日")
  def dayOfWeekLabel: LocalDate => Parser[LocalDate] = { date =>
    val label = dayOfWeekLabels(date.dayOfWeek.get-1)
    val holidayLabel = "・祝".r
    openp ~> (label.r <~ holidayLabel.?) <~ closep ^^ {_ => date}
  }
  
  def year: Parser[Int]  = """\d\d\d\d""".r ^^ {_.toInt}
  def month: Parser[Int] = """\d+""".r ^^ {_.toInt}
  def day: Parser[Int]   = """\d+""".r ^^ {_.toInt}
  def yearMonth: Parser[YearMonth] = year ~ slash ~ month ^^ { 
    case y ~ _ ~ m => new YearMonth(y, m)
  }
  def date: Parser[LocalDate] = yearMonth ~ slash ~ day ^^ { 
    case ym ~ _ ~ d => ym.toLocalDate(d)
  }
  def dateEx: Parser[LocalDate] = date >> dayOfWeekLabel

  def jdate: Parser[LocalDate] = month ~ "月" ~ day ~ "日" ^^ {
    case month ~ _ ~ day ~ _ => LocalDate.now.withMonthOfYear(month).withDayOfMonth(day)
  }
  def jdateEx: Parser[LocalDate] = jdate >> dayOfWeekLabel

  def parseDateEx(input: String): LocalDate = parseAll(dateEx, input) match {
    case Success(result, _) => result
    case fail: NoSuccess => sys.error(fail.msg)
  }

  // -----------------------------------------------
  //  parsers for title
  // -----------------------------------------------
  def subtitle: Parser[String] = """[^()]+""".r
  def title: Parser[(String, YearMonth)] = subtitle ~ openp ~ yearMonth ~ closep ^^ {
    case subtitle ~ _ ~ yearMonth ~ _ => (subtitle, yearMonth)
  }

  def parseTitle(input: String): (String, YearMonth) = parseAll(title, input) match {
    case Success(result, _) => result
    case fail: NoSuccess => sys.error(fail.msg)
  }
  
  // -----------------------------------------------
  //  parsers for description
  // -----------------------------------------------
  def shinnendoDesc: Parser[String] = """.*新年度を迎えました.*""".r
  def introDesc: Parser[Int] = month <~ """月の参加(申込|申し込み)[^\d]*""".r
  def eventIntroDesc: Parser[Int] = month <~ """月の開催[^\d]*""".r
  def eventDatesDesc: Parser[Seq[LocalDate]] = rep((dateEx <~ sep.?))
  def numEventsDesc: Parser[Int] = """[^\d]*""".r ~> """\d+""".r <~ """回.*""".r ^^ { _.toInt }
  def dueApplyLabel: Parser[String] = """(申込|申し込み)期限[^\d]*""".r
  def dueApplyDesc: Parser[LocalDate] = dueApplyLabel ~> (dateEx | jdateEx) <~ rest
  def restOfAll =  rest ~ success(null)

  def description = shinnendoDesc.? ~ introDesc.? ~ eventIntroDesc ~ eventDatesDesc ~
                     numEventsDesc ~ dueApplyDesc ~ restOfAll ^^ {
    case shinnendoDesc ~ introDesc ~ eventMonth ~ eventDates ~ numEvents ~ dueDate ~ _ => 
      Descr(shinnendoDesc.isDefined, eventMonth, eventDates, numEvents, dueDate)
  }

  def parseDesc(input: String): Descr = parseAll(description, input) match {
    case Success(result, _) => result
    case fail: NoSuccess => sys.error(fail.msg)
  }
}

case class Descr(shinnendo: Boolean, month: Int, eventDates: Seq[LocalDate], numEvents: Int, dueDate: LocalDate)
