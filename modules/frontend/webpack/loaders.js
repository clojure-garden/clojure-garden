const __MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CleanCSSPlugin = require("less-plugin-clean-css");

const mode = require("./mode");
const paths = require("./paths");

const _MiniCssExtractPluginLoader = {
  loader: __MiniCssExtractPlugin.loader,
  options: {
    esModule: false,
  },
};

const _LessLoader = {
  loader: "less-loader",
  options: {
    sourceMap: mode.isProduction,
    lessOptions: {
      javascriptEnabled: true,
      webpackImporter: false,
      plugins: [new CleanCSSPlugin({ advanced: mode.isProduction })],
    },
  },
};

const _CssLoader = { loader: "css-loader", options: { importLoaders: 1, sourceMap: mode.isProduction } };

const _StyleLoader = "style-loader";

const _PostCssLoader = {
  loader: "postcss-loader",
  options: {
    config: {
      path: paths.postcssConfigPath,
    },
  },
};

const JsSourceMapLoader = {
  test: /\.js$/,
  enforce: "pre",
  use: ["source-map-loader"],
};

const LessLoader = {
  test: /\.less$/,
  use: [_StyleLoader, _MiniCssExtractPluginLoader, _CssLoader, _LessLoader],
};

const CssLoader = {
  test: /\.css$/,
  use: [_StyleLoader, _MiniCssExtractPluginLoader, _CssLoader, _PostCssLoader],
};

const FontsLoader = {
  test: /\.(woff2?|ttf|eot)(\?v=\d+\.\d+\.\d+)?$/i,
  type: "asset/resource",
};

const ImagesLoader = {
  test: /\.(svg|png|jpe?g|gif)(\?v=\d+\.\d+\.\d+)?$/i,
  type: "asset/resource",
};

module.exports = {
  JSSourceMapLoader: JsSourceMapLoader,
  LessLoader: LessLoader,
  CSSLoader: CssLoader,
  FontsLoader: FontsLoader,
  ImagesLoader: ImagesLoader,
};
