module.exports = {
  parser: "sugarss",
  plugins: {
    autoprefixer: {},
    "postcss-import": {},
    "postcss-preset-env": {
      browsers: "last 2 versions",
    },
    cssnano: {},
  },
};
