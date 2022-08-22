FROM ghcr.io/afritzler/mkdocs-material

# Ensure installation of required packages
# - uses updated cache on-the-fly without storing it locally
RUN apk --no-cache add \
    git

# Install additional plugins
# - mkdocs-git-revision-date-localized-plugin - Add last updated date to site using git revision
# - mkdocs-glightbox - Add lightboxes for images, videos, etc.
RUN python -m pip install --no-cache-dir \
    mkdocs-git-revision-date-localized-plugin \
    mkdocs-glightbox

# Trust git directory for git revision plugin
RUN git config --global --add safe.directory /docs
