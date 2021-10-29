# FenixEdu Id Cards Frontend

> The FenixEdu ID Cards frontend application

### Requirements

- Node.js & NPM

### Node and NPM versions

FenixEdu ID Cards application works using the latest `v12.x` version of nodejs and the latest `v6.x` version of npm.

Last versions tested on this project:

- Node: `v12.18.3`
- NPM: `v6.14.6`

#### Checking your Node and NPM versions

**Node**:

```bash
# check node version
node -v
```

E.g. `v12.18.3`.

**NPM**:

```bash
# check npm version
npm -v
```

E.g. `v6.14.6`.

**If you're having problems with your version, try to use [nvm](https://github.com/creationix/nvm/blob/master/README.md) to install a specific version of node and npm.**

**Installing using [nvm](https://github.com/creationix/nvm/blob/master/README.md)**:

```bash
# install nodejs 12.x
nvm install 12
```

This will install the latest `12.x` node version with the latest npm `6.x` version.

### Available npm scripts

```bash
# install dependencies
npm install

# serve with hot reload at localhost:8081
npm start

# serve (context mode) with hot reload at localhost:8081
npm run serve:context

# build for production with minification
npm run package:production

# build for staging with minification
npm run package:staging

# clean to remove node_modules and package-lock.json
npm run clean:packages

# clean to remove dist and build inside webapp
npm run clean:dist

# lint javascript
npm run lint:js

# lint scss
npm run lint:scss

# format (javascript + scss lint & format)
npm run format
```
