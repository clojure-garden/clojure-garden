const path = require("path");

const projectRootPath = path.resolve(__dirname, "../");

const resourcesPath = path.join(projectRootPath, "public");
const assetsPath = path.join(resourcesPath, "assets");
const fontsPath = "fonts";
const imagesPath = "images";

const postcssConfigPath = path.join(projectRootPath, "postcss.config.js");

console.info("Project root path: ", projectRootPath);
console.info("Resources path: ", resourcesPath);
console.info("Assets path: ", assetsPath);

console.info("PostCSS config path: ", postcssConfigPath);

module.exports = {
  rootPath: projectRootPath,
  resourcesPath: resourcesPath,
  assetsPath: assetsPath,
  fontsPath: fontsPath,
  imagesPath: imagesPath,
  postcssConfigPath: postcssConfigPath,
};
