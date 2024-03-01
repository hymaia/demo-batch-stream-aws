package fr.hymaia

import com.github.tototoshi.csv._
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.DriverManager
import scala.collection.mutable.ListBuffer
import scala.sys.env

case class Connection(id: Int, ip: String, datetime: String, url: String)

object Ingestion {
  def main(args: Array[String]): Unit = {
    val dbSecret = DbSecret.getSecret
    val dbHost = env.getOrElse("DB_ENDPOINT", "")
    val dbPort = env.getOrElse("DB_PORT", "3306")
    val dbName = env.getOrElse("DB_NAME", "hymaiaDB")

    val url = s"jdbc:mysql://$dbHost:$dbPort/$dbName?rewriteBatchedStatements=true"
    val username = dbSecret.username
    val password = dbSecret.password

    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection = DriverManager.getConnection(url, username, password)

    try {
      // Création de la table
      val selectSqlRequest =
        """
          |SELECT * FROM user_connections;
          |""".stripMargin

      val resultSet = connection.createStatement().executeQuery(selectSqlRequest)

      val connections = new ListBuffer[Connection]()

      while (resultSet.next()) {
        val id = resultSet.getInt("id")
        val ip = resultSet.getString("ip")
        val timestamp = resultSet.getString("datetime")
        val url = resultSet.getString("url")

        connections += Connection(id, ip, timestamp, url)
      }

      val outputStream = new ByteArrayOutputStream()
      val writer = CSVWriter.open(outputStream)

      // Écriture de l'en-tête
      writer.writeRow(List("IP", "URL", "DateTime"))

      // Écriture des données
      connections.foreach { data =>
        writer.writeRow(List(data.ip, data.url, data.datetime.toString))
      }

      writer.close()

      val byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray)
      val bucketName = env.getOrElse("BUCKET_NAME", "")
      val fileKey = "connections/data.csv"

      uploadToS3(bucketName, fileKey, byteArrayInputStream, outputStream.size())

    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      connection.close()
    }
  }

  def uploadToS3(bucketName: String, fileKey: String, inputStream: ByteArrayInputStream, contentLength: Long): Unit = {
    val s3 = S3Client.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.EU_WEST_1) // Changez la région selon vos besoins
      .build()

    val putObjectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(fileKey)
      .build()

    s3.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, contentLength))

    println(s"File uploaded to $bucketName/$fileKey")
  }
}
