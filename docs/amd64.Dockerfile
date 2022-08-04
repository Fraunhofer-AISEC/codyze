FROM squidfunk/mkdocs-material

# add git-revision-date-localized plugin
RUN apk update && \
    apk add git
RUN python -m pip install --no-cache-dir \
  mkdocs-git-revision-date-localized-plugin

# add glightbox plugin
RUN python -m pip install --no-cache-dir \
  mkdocs-glightbox