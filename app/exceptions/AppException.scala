package exceptions

case class AppException(msg: String = "Application exception") extends RuntimeException(msg)
