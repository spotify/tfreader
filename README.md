# tfreader ![](https://github.com/spotify/tfreader/workflows/main/badge.svg)

Simple CLI tool to read `TensorFlow` `TFRecords`.

## Install

### MacOs
```bash
brew tap spotify/public
brew install tfreader
```

### Linux

Right now we only have binaries available under [releases](https://github.com/spotify/tfreader/releases)

## Usage

```
tfr
Usage: tfr [options] <files? | STDIN>
  --usage  <bool>
        Print usage and exit
  --help | -h  <bool>
        Print help message and exit
  --check-crc32  <bool>
        If enabled checks CRC32 on each record
  --number | -n  <int?>
        Number of records to output
```

### Example

```bash
tfr -n 1 core/src/test/resources/part-00000-of-00004.tfrecords | jq .
```

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
            "60"
          ]
        }
      },
      "payment_type": {
        "bytesList": {
          "value": [
            "Q2FzaA=="
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
            "1402934400"
          ]
        }
      },
      "trip_start_day": {
        "int64List": {
          "value": [
            "2"
          ]
        }
      },
      "trip_start_hour": {
        "int64List": {
          "value": [
            "16"
          ]
        }
      },
      "trip_start_month": {
        "int64List": {
          "value": [
            "6"
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
            "MTcwMzEwODE4MDA="
          ]
        }
      },
      "dropoff_community_area": {
        "bytesList": {
          "value": [
            "OA=="
          ]
        }
      },
      "pickup_community_area": {
        "bytesList": {
          "value": [
            "OA=="
          ]
        }
      },
      "trip_id": {
        "bytesList": {
          "value": [
            "ODEwNmMxZjYtZTZmMy00MjZmLTlhYWYtYjRlOTcwM2I0ZjEw"
          ]
        }
      }
    }
  }
}
```
