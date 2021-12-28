const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const AntdDayJSWebpackPlugin = require("antd-dayjs-webpack-plugin");

const miniCssExtractPluginOptions = {
  filename: "bundle.css",
};

const terserPluginOptions = {
  test: /\.js(\?.*)?$/i,
  parallel: true,
};

const cssMinimizerPluginOptions = {};

const antdDayJSWebpackPluginOptions = { replaceMoment: true };

module.exports = {
  MiniCssExtractPlugin: new MiniCssExtractPlugin(miniCssExtractPluginOptions),
  TerserPlugin: new TerserPlugin(terserPluginOptions),
  CssMinimizerPlugin: new CssMinimizerPlugin(cssMinimizerPluginOptions),
  AntdDayJSWebpackPlugin: new AntdDayJSWebpackPlugin(antdDayJSWebpackPluginOptions),
};
