(ns lenskit-sample.core
  (:import (java.io File)
           (org.grouplens.lenskit ItemScorer)
           (org.grouplens.lenskit.baseline BaselineScorer
                                           ItemMeanRatingItemScorer
                                           UserMeanBaseline
                                           UserMeanItemScorer)
           (org.grouplens.lenskit.core LenskitConfiguration
                                       LenskitRecommender)
           (org.grouplens.lenskit.data.dao EventDAO SimpleFileRatingDAO)
           (org.grouplens.lenskit.knn.user UserUserItemScorer)
           (org.grouplens.lenskit.transform.normalize BaselineSubtractingUserVectorNormalizer
                                                      UserVectorNormalizer)))

(defn create-lenskit-config
  [filename]
  (let [config (LenskitConfiguration.)]
    (doto config
      (-> (.bind ItemScorer) (.to UserUserItemScorer))
      (-> (.bind BaselineScorer ItemScorer) (.to UserMeanItemScorer))
      (-> (.bind UserMeanBaseline ItemScorer) (.to ItemMeanRatingItemScorer))
      (-> (.bind UserVectorNormalizer) (.to BaselineSubtractingUserVectorNormalizer))
      (-> (.bind EventDAO) (.to (SimpleFileRatingDAO. (File. filename) "\t"))))))

(defn create-recommender
  [config]
  (LenskitRecommender/build config))

(defn recommend
  [recommender userid count]
  (let [item-recommender (.getItemRecommender recommender)]
    (.recommend item-recommender userid count)))

(defn recommend-item-ids
  [userid]
  (let [recommend-count 10
        config (create-lenskit-config "resources/ml-100k/u.data")
        recommender (create-recommender config)]
    (recommend recommender userid recommend-count)))

(defn -main
  [& args]
  (let [userid (first args)]
    (if (nil? userid)
      (println "usage: lein run [userid]")
      (println (map #(.getId %) (recommend-item-ids (read-string userid)))))))
