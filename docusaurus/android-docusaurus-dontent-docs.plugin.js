module.exports = {
  plugins: [
    [
      '@docusaurus/plugin-content-docs',
      {
        lastVersion: 'current',
        versions: {
          current: {
            label: 'v6',
          },
          '5.x.x': {
            label: 'v5',
            path: 'v5',
            banner: 'unmaintained'
          },
          'draft': {
            label: 'Draft',
            path: 'draft',
            banner: 'unreleased'
          },
        }
      }
    ]
  ]
}