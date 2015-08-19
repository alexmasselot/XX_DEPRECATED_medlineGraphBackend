package ch.twenty.medlineGraph.tools

/**
 * @author Alexandre Masselot.
 */
object SparkTest extends App{
  import org.apache.spark.{SparkContext, SparkConf}


  val conf = new SparkConf(false)
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("just a worksheet") // name in Spark web UI
    .set("spark.logConf", "true")

  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  val path = "/Users/amasselo/private/dev/medline-graph/data/spark/processed/citations-with-coordinates-actual.parquet"
  val citations = sqlContext.read.parquet("/Users/amasselo/private/dev/medline-graph/data/spark/processed/citations-with-coordinates-actual.parquet")
  citations.printSchema()


}
