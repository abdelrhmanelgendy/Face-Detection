def perform_and(x, y):
    return x and y


def perform_or(x, y):
    return x or y


def perform_xor(x, y):
    return x != y


def perform_not(x):
    return not x


def perform_logic_operation(choice, x, y=None):
    if choice == '1':
        return perform_and(x, y)
    elif choice == '2':
        return perform_or(x, y)
    elif choice == '3':
        return perform_xor(x, y)
    elif choice == '4':
        return perform_not(x)
    else:
        print("Invalid choice. Please enter 1, 2, 3, or 4.")


def main(choice, x=True, y=False):
    # Set arguments for the logic operation
    # choice = '1'  # Choose the logic operation (1 for AND, 2 for OR, 3 for XOR, 4 for NOT)
    # x = True      # Set the first operand
    # y = False     # Set the second operand (set to None if not needed for NOT operation)
    print(f"we will run with: choice={choice} , x={x} , y={y}")
    # Perform the logic operation and return the result
    result = perform_logic_operation(choice, x, y)
    return result


if __name__ == "__main__":
    result = main(1, True, False)
    print(f"Result of the logic operation: {result}")
