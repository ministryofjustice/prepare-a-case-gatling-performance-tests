package util

import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtils {
  def today(): String = {
    val now = Calendar.getInstance().getTime
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    simpleDateFormat.format(now)
  }

  def timeNow(): String = {
    val now = Calendar.getInstance().getTime
    val simpleDateFormat = new SimpleDateFormat("HH:mm")
    simpleDateFormat.format(now)
  }

  def OffenceDate(): String = {
    val calender = Calendar.getInstance()
    calender.add(Calendar.DATE, -7)
    val simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
    simpleDateFormat.format(calender.getTime)
  }

  def ConvicationDate(): String = {
    val calender = Calendar.getInstance()
    calender.add(Calendar.DATE, -2)
    val simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
    simpleDateFormat.format(calender.getTime)
  }

  def FutureDate(): String = {
    val calender = Calendar.getInstance()
    calender.add(Calendar.DATE, +5)
    val simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
    simpleDateFormat.format(calender.getTime())
  }

  def TimeStamp(): String = {
    val time: java.util.Date = Calendar.getInstance.getTime
    val simpleDateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss")
    simpleDateFormat.format(time)
  }

  def formatSeconds(s: Int): String = {
    if (s == 0) return "once"
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    hours + "h " + minutes + "m " + seconds + "s"
  }
}
