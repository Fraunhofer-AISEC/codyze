---
title: "Contributing to Codyze"
linkTitle: "Contributing to Codyze"
no_list: true
weight: 10
description: >
  Contributing to Codyze and updating this documentation
---

{{% pageinfo %}}
Codyze is open source and an ongoing effort to support developers and auditors in improving the quality of security-critical code. We welcome everybody to contribute to this project, be it fixing a typo in the documentation or implementing whole new language support and analysis modules.

Please note that parts of this Codyze are backed by research projects. If you plan to make larger contributions, please contact us upfront to align your plans with possibly ongoing projects.
{{% /pageinfo %}}


## Contributing to this documentation

This documentation lives in the same repository as the code and the idea is that both are always consistent. If you make changes in the code that affect the documentation, please update the documentation in the same pull request.

To extend or fix errors in this documentation, proceed as follows:

1. Fork the [Codyze repo](https://github.com/Fraunhofer-AISEC/codyze) on GitHub.
1. Make your changes and send a pull request (PR).
1. If you're not yet ready for a review, add "WIP" to the PR name to indicate 
  it's a work in progress. (**Don't** add the Hugo property 
  "draft = true" to the page front matter, because that prevents the 
  auto-deployment of the content preview described in the next point.)
1. Wait for the automated PR workflow to do some checks. When it's ready,
  you should see a comment like this: **deploy/netlify — Deploy preview ready!**
1. Click **Details** to the right of "Deploy preview ready" to see a preview
  of your updates.
1. Continue updating your doc and pushing your changes until you're happy with 
  the content.
1. When you're ready for a review, add a comment to the PR, and remove any
  "WIP" markers.

### Previewing your changes locally

If you want to run your own local Hugo server to preview your changes as you work:

1. Install Hugo and any other tools you need. You'll need at least **Hugo version 0.45** (we recommend using the most recent available version), and it must be the **extended** version, which supports SCSS.
1. Fork the [Codyze repo](https://github.com/Fraunhofer-AISEC/codyze) repo into your own project, then create a local copy using `git clone`. Don’t forget to use `--recurse-submodules` or you won’t pull down some of the code you need to generate a working site.

    ```
    git clone --recurse-submodules --depth 1 https://github.com/google/docsy-example.git
    ```

1. Run `hugo server` in the site root directory. By default your site will be available at http://localhost:1313/. Now that you're serving your site locally, Hugo will watch for changes to the content and automatically refresh your site.
1. Continue with the usual GitHub workflow to edit files, commit them, push the
  changes up to your fork, and create a pull request.

### Creating an issue

If you've found a problem in the docs, but you're not sure how to fix it yourself, please create an issue in the [Codyze repo](https://github.com/Fraunhofer-AISEC/codyze/issues) and add the label `documentation` to it. You can also create an issue about a specific page by clicking the **Create Issue** button in the top right hand corner of the page.

## Contributing to Codyze

### Opening an issue

We welcome any bug reports! Please use the issue tracker only to report bugs or unexpected behavior. If you have general questions about the project or need help in setting it up, please reach out to us directly and **do not** the issue tracker.

To report a bug, make sure to include the following information:

1. What have you done? Provide the minimal set of files needed to reproduce the bug. If we cannot reproduce it, it's not a bug.
1. What outcome have you expected, what outcome did you get?
1. Can you provide any hints on what might be the cause of the bug and how you think it should be fixed?


### Useful resources

* [Docsy user guide](https://github.com/google/docsy): All about Docsy, the documentation template we use, including how it manages navigation, look and feel, and multi-language support.
* [Hugo documentation](https://gohugo.io/documentation/): Comprehensive reference for Hugo.
* [Github Hello World!](https://guides.github.com/activities/hello-world/): A basic introduction to GitHub concepts and workflow.


