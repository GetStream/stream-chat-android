module.exports = {
  plugins: [
    [
      '@docusaurus/plugin-content-docs',
      {
        lastVersion: '5.x.x',
        versions: {
          current: {
            label: 'v6-Beta',
            path: 'next'
          },
          '5.x.x': {
            label: 'v5',
            banner: 'none'
          }
        }
      }
    ]
  ]
}