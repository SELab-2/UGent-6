import argparse
from request import Requester




def parse_arguments():
    parser = argparse.ArgumentParser(description='Process accesstoken for sample data')
    parser.add_argument('token', type=str, required=True,
                        help='The microsoft JWT access token to call the backend')
    return parser.parse_args()



if __name__ == '__main__':
    args = parse_arguments()
    req = Requester(args.token)

