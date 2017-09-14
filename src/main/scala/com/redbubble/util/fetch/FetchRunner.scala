package com.redbubble.util.fetch

import com.redbubble.util.cache.{CacheKey, SimpleCache}
import com.redbubble.util.fetch.TwitterFutureFetchMonadError.twitterFutureFetchMonadError
import com.twitter.util.{Await, Future, FuturePool}
import fetch._

final case class FetchedObjectCache(underlying: SimpleCache) extends DataSourceCache {
  override def get[A](k: DataSourceIdentity) = {
    val valueFuture = underlying.get[A](CacheKey(k.toString))
    Await.result(valueFuture)
  }

  override def update[A](k: DataSourceIdentity, v: A) = {
    underlying.put(CacheKey(k.toString()), v)
    this
  }
}

final case class FetcherRunner(c: SimpleCache)(implicit fp: FuturePool) {
  private val cache = FetchedObjectCache(c)

  /**
    * Runs this fetch as a `Future`, returning a `Future` containing the result.
    */
  def runFetchF[T](fetch: Fetch[T]): Future[T] =
    Fetch.run[Future](fetch, cache)(twitterFutureFetchMonadError)
}
