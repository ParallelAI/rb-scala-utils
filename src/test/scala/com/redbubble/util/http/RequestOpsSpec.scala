package com.redbubble.util.http

import com.redbubble.util.io.BufOps._
import com.redbubble.util.io.Charset.DefaultCharset
import com.redbubble.util.spec.SpecHelper
import io.circe._
import io.finch.Decode
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.specs2.mutable.Specification

final class RequestOpsSpec extends Specification with SpecHelper {

  case class Bar(baz: String)

  implicit val barDecoder: Decoder[Bar] = Decoder.instance { c =>
    c.downField("bar").downField("baz").as[String].map { b => Bar(b) }
  }
  val barDecode: Decode.Json[Bar] = RequestOps.decodeDataJson[Bar](barDecoder)

  val objectRequestProp = new Properties("Object request decoding") {
    property("decode") = forAll(Gen.alphaStr) { (s: String) =>
      val barJsonRequestBuf = stringToBuf(s"""{"data":{"bar":{"baz":"$s"}}}""")
      val decodedBar = barDecode.apply(barJsonRequestBuf, DefaultCharset)
      decodedBar.toOption must beSome(Bar(s))
    }
  }

  s2"Objects with a Decooder instance can be decoded from a JSON request$objectRequestProp"
}
