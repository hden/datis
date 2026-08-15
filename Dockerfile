FROM clojure:temurin-21-tools-deps-jammy

WORKDIR /app
COPY deps.edn .
RUN clojure -P -M:duct

COPY . .

EXPOSE 3000
CMD ["clojure", "-M:duct", "--main", "--keys", ":duct/daemon"]
