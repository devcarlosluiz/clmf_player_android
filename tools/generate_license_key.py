import hmac
import hashlib
import sys

LICENSE_SECRET = "50a733709fa9fc397a720c3179b0693081e51fe3415b6b31"


def generate_key(device_id: str) -> str:
    digest = hmac.new(
        LICENSE_SECRET.encode("utf-8"),
        device_id.encode("utf-8"),
        hashlib.sha256,
    ).hexdigest().upper()
    raw = digest[:16]
    return "-".join(raw[i:i + 4] for i in range(0, len(raw), 4))


def main() -> None:
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)

    device_id = sys.argv[1].strip()
    key = generate_key(device_id)
    print(f"ID do dispositivo: {device_id}")
    print(f"Chave de ativação: {key}")


if __name__ == "__main__":
    main()
