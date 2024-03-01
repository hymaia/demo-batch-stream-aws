package fr.hymaia

import java.sql.{DriverManager, PreparedStatement}
import scala.sys.env
import scala.util.Random

object CreateData {
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
      println("suppression de la table user_connections")
//      connection.createStatement().execute("DROP TABLE user_connections")
      val createTableStatement = connection.createStatement()
      val createTableSql =
        """
          |CREATE TABLE IF NOT EXISTS user_connections (
          |  id INT AUTO_INCREMENT PRIMARY KEY,
          |  ip VARCHAR(255) NOT NULL,
          |  datetime DATETIME NOT NULL,
          |  url VARCHAR(255) NOT NULL
          |);
          |""".stripMargin
          println("création de la table user_connections")
      createTableStatement.execute(createTableSql)

      // Insertion des données en batch
      val insertSql = "INSERT INTO user_connections (ip, datetime, url) VALUES (?, ?, ?);"
      val preparedStatement: PreparedStatement = connection.prepareStatement(insertSql)

      val batchSize = 10000

      println(s"insertion de la donnée dans la table par batch de $batchSize lignes")
      (1 to 100000).foreach { i =>
        preparedStatement.setString(1, s"${Random.nextInt(255)}.${Random.nextInt(255)}.${Random.nextInt(255)}.${Random.nextInt(255)}")
        preparedStatement.setString(2, s"2024-01-${Random.nextInt(2)}${Random.nextInt(9)} ${Random.nextInt(1)}${Random.nextInt(9)}:${Random.nextInt(5)}${Random.nextInt(9)}:${Random.nextInt(5)}${Random.nextInt(9)}")
        preparedStatement.setString(3, s"example.com/${Random.nextIt(50)}")
        preparedStatement.addBatch()

        if ((i % batchSize) == 0) {
          println(s"execute batch $i")
          preparedStatement.executeBatch() // Exécuter le batch
          preparedStatement.clearBatch()
        }
      }
      println("fini !")

    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      connection.close()
    }
  }
}
