package security

import org.mindrot.jbcrypt.BCrypt

object PasswordService {

  def createPassword(clearString: String): String = {
    BCrypt.hashpw(clearString, BCrypt.gensalt)
  }

  def checkPassword(candidate: String, encryptedPassword: String): Boolean = {
    BCrypt.checkpw(candidate, encryptedPassword)
  }


}
