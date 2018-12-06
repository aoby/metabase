(ns metabase.driver.sqlite-test
  (:require [expectations :refer [expect]]
            [metabase.test.data.datasets :refer [expect-with-driver]]
            [metabase.test.util :as tu]))

(println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
(println "!!                                                                            !!")
(println "!!                                    HERE                                    !!")
(println "!!                                                                            !!")
(println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

;; a basic test to make sure we can actually test this driver (!)
(expect false)

(expect-with-driver :sqlite
  "UTC"
  (tu/db-timezone-id))
