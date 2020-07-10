# tfreader ![](https://github.com/spotify/tfreader/workflows/main/badge.svg)

Simple native CLI tool to read `TensorFlow` `TFRecords`.

## Install

### MacOs

```bash
brew tap spotify/public
brew install tfreader
```

### Linux

Right now we only have binaries available under [releases](https://github.com/spotify/tfreader/releases)

## Usage

```bash
tfr
Usage: tfr [options] <files? | STDIN>
  --usage  <bool>
        Print usage and exit
  --help | -h  <bool>
        Print help message and exit
  --record | -r  <string>
        What type of record should be read. Default is `example`.
  --check-crc32  <bool>
        If enabled checks CRC32 on each record
  --number | -n  <int?>
        Number of records to output
  --flat | -f  <bool>
        Output examples as flat JSON objects
```

## Examples

#### Google Cloud Storage

```bash
tfr -n 1 gs://<bucket>/<path>/part-00000-of-00004.tfrecords | jq .
```

#### Local Filesystem

```bash
tfr -n 1 core/src/test/resources/part-00000-of-00004.tfrecords | jq .
```

#### `stdin`

```bash
cat core/src/test/resources/part-00000-of-00004.tfrecords | tfr -n 1 | jq .
```

### Output

#### Flat

```json
{
  "tips": [
    0
  ],
  "trip_seconds": [
    60
  ],
  "payment_type": [
    "Cash"
  ],
  "trip_miles": [
    0
  ],
  "dropoff_longitude": [
    -87.63785
  ],
  "dropoff_latitude": [
    41.893215
  ],
  "pickup_longitude": [
    -87.63187
  ],
  "pickup_latitude": [
    41.89204
  ],
  "trip_start_timestamp": [
    1402934400
  ],
  "trip_start_day": [
    2
  ],
  "trip_start_hour": [
    16
  ],
  "trip_start_month": [
    6
  ],
  "fare": [
    3.25
  ],
  "dropoff_census_tract": [
    "17031081800"
  ],
  "dropoff_community_area": [
    "8"
  ],
  "pickup_community_area": [
    "8"
  ],
  "trip_id": [
    "8106c1f6-e6f3-426f-9aaf-b4e9703b4f10"
  ]
}
```

#### Default

```json
{
  "features": {
    "feature": {
      "tips": {
        "floatList": {
          "value": [
            0
          ]
        }
      },
      "trip_seconds": {
        "int64List": {
          "value": [
            60
          ]
        }
      },
      "payment_type": {
        "bytesList": {
          "value": [
            "Cash"
          ]
        }
      },
      "trip_miles": {
        "floatList": {
          "value": [
            0
          ]
        }
      },
      "dropoff_longitude": {
        "floatList": {
          "value": [
            -87.63785
          ]
        }
      },
      "dropoff_latitude": {
        "floatList": {
          "value": [
            41.893215
          ]
        }
      },
      "pickup_longitude": {
        "floatList": {
          "value": [
            -87.63187
          ]
        }
      },
      "pickup_latitude": {
        "floatList": {
          "value": [
            41.89204
          ]
        }
      },
      "trip_start_timestamp": {
        "int64List": {
          "value": [
            1402934400
          ]
        }
      },
      "trip_start_day": {
        "int64List": {
          "value": [
            2
          ]
        }
      },
      "trip_start_hour": {
        "int64List": {
          "value": [
            16
          ]
        }
      },
      "trip_start_month": {
        "int64List": {
          "value": [
            6
          ]
        }
      },
      "fare": {
        "floatList": {
          "value": [
            3.25
          ]
        }
      },
      "dropoff_census_tract": {
        "bytesList": {
          "value": [
            "17031081800"
          ]
        }
      },
      "dropoff_community_area": {
        "bytesList": {
          "value": [
            "8"
          ]
        }
      },
      "pickup_community_area": {
        "bytesList": {
          "value": [
            "8"
          ]
        }
      },
      "trip_id": {
        "bytesList": {
          "value": [
            "8106c1f6-e6f3-426f-9aaf-b4e9703b4f10"
          ]
        }
      }
    }
  }
}
```
