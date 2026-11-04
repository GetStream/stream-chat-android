# Test fixtures

| Folder | What it holds |
| --- | --- |
| `conformance/core` | The A2UI conformance suites this renderer runs (`conformance/core/*.yaml` of [a2ui-project/a2ui](https://github.com/a2ui-project/a2ui) at commit `d378d20`), converted from YAML to JSON unchanged. |
| `conformance/catalogs` | The suite's 1.0 catalog fixtures (`conformance/test_data/catalogs/*_v1_0.json`), unchanged. |
| `examples` | The examples published with the A2UI 1.0 basic catalog (`catalogs/basic/v1/examples`), unchanged; `index.txt` lists them. |
| `fixtures` | The restaurant demo's backend payloads from [GetStream/chat-ai-samples](https://github.com/GetStream/chat-ai-samples), upgraded from A2UI v0.9 to 1.0: `version` 1.0, `@path` and `@call`, the 1.0 basic catalog id, ids as identifiers, and headings in Markdown instead of the `h1`–`h5` variants. |

To refresh the conformance suites, convert each YAML file to JSON, for example:

```python
import json, yaml
json.dump(yaml.safe_load(open("expressions.yaml")), open("expressions.json", "w"), ensure_ascii=False, indent=1)
```

A2UI is Apache 2.0 licensed; see `THIRD_PARTY_NOTICES` in this module.
