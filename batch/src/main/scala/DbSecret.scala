package fr.hymaia

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.sys.env


case class DbSecret(username: String, password: String)

object DbSecret {
  implicit val formats = DefaultFormats
  val region = Region.EU_WEST_1 // Changez cette valeur selon votre région AWS
  val secretName = env.getOrElse("DB_SECRET", "")

  def getSecret: DbSecret = {
    val client = SecretsManagerClient.builder()
      .region(region)
      .build()

    val getSecretValueRequest = GetSecretValueRequest.builder()
      .secretId(secretName)
      .build()

    val secretValue = client.getSecretValue(getSecretValueRequest)
    client.close()

    val json = parse(secretValue.secretString())
    val dbSecret = json.extract[DbSecret]
    dbSecret
  }
}
