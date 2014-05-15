package machisoccer

import com.github.nscala_time.time.Imports._
//import org.joda.time._

import scala.util.parsing.combinator._

object MachisoccerWeb extends RegexParsers {
  def sep: Parser[String] = """[,、]""".r
  def year: Parser[Int]  = """\d\d\d\d""".r ^^ {_.toInt}
  def month: Parser[Int] = """\d+""".r ^^ {_.toInt}
  def day: Parser[Int]   = """\d+""".r ^^ {_.toInt}
  def yearMonth: Parser[YearMonth] = year ~ "/" ~ month ^^ { 
    case y ~ _ ~ m => new YearMonth(y, m)
  }
  def date: Parser[LocalDate] = yearMonth ~ "/" ~ day ^^ { 
    case ym ~ _ ~ d => ym.toLocalDate(d)
  }
  def dateEx: Parser[DateEx] = date ~ """\(\s\)""".r ^^ {
    case date ~ dayOfWeek => DateEx(date, dayOfWeek)
  }

  def subtitle: Parser[String] = """[^()]+""".r
  def title: Parser[(String, YearMonth)] = subtitle ~ "(" ~ yearMonth ~ ")" ^^ {
    case subtitle ~ _ ~ yearMonth ~ _ => (subtitle, yearMonth)
  }

  def parseTitle(input: String): (String, YearMonth) = parseAll(title, input) match {
    case Success(result, _) => result
    case fail: NoSuccess => sys.error(fail.msg)
  }
  
  def shinnendoDesc: Parser[String] = """.*新年度を迎えました.*""".r
  def eventIntroDesc: Parser[Int] = """.*(\d+)月の開催[^\d]*""".r ^^ {_.toInt}
  def eventDatesDesc: Parser[Seq[DateEx]] = rep((dateEx <~ sep.?))
  def numEventsDesc: Parser[Int] = """[^\d]*(\d+)回.*""".r ^^ {_.toInt}
  def dueApplyDesc: Parser[DateEx] = "申し込み期限：" ~> dateEx <~ """.*""".r 

  def description = shinnendoDesc.? ~ eventIntroDesc ~ eventDatesDesc ~ numEventsDesc ~ dueApplyDesc ^^ {
    case shinnendoDesc ~ eventMonth ~ eventDates ~ numEvents ~ dueDate => 
      Descr(shinnendoDesc.isDefined, eventMonth, eventDates, numEvents, dueDate)
  }

  def parseDesc(input: String): Descr = parseAll(description, input) match {
    case Success(result, _) => result
    case fail: NoSuccess => sys.error(fail.msg)
  }
}

case class DateEx(localDate: LocalDate, dayOfWeek: String)
case class Descr(shinnendo: Boolean, month: Int, eventDates: Seq[DateEx], numEvents: Int, dueDate: DateEx)
