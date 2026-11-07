# saas-project

A responsive, dark-mode mobile GitHub repository mockup based on the provided reference screens. It includes a repository overview and a Pull Requests view, with lightweight client-side interactions.

## Run locally

No dependencies or build step are required. Open `index.html` in a browser, or start a static server from this directory:

```sh
python3 -m http.server 8000
```

Then visit `http://localhost:8000`.

## Included interactions

- Navigate between the repository overview and Pull Requests.
- Toggle the repository star and watch state.
- Open filter menus, search pull requests, and switch the displayed branch.
- Create a pull request locally with the new-pull-request form.

Pull requests and repository changes are in-memory only and reset when the page reloads.
