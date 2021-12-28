const mode = require("./mode");
const paths = require("./paths");
const loaders = require("./loaders");
const plugins = require("./plugins");

module.exports = {
  mode: mode.currentMode,
  entry: ["./main.js"],
  output: {
    path: paths.assetsPath,
    filename: "bundle.js",
  },
  optimization: {
    minimize: mode.isProduction,
    minimizer: [plugins.TerserPlugin, plugins.CssMinimizerPlugin],
  },
  plugins: [plugins.MiniCssExtractPlugin],
  module: {
    rules: [loaders.JSSourceMapLoader, loaders.FontsLoader, loaders.ImagesLoader, loaders.CSSLoader, loaders.LessLoader],
  },
};
