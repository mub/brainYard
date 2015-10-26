# input: 4 digits of the same length for this implementation, first two are parts of the first multiplier and the last two are parts of the second
def karaMul(a, b, c, d)
    n = a.length
    fullMul = 10 ** (n*2)
    halfMul = 10 ** n
    ai, bi, ci, di = a.to_i, b.to_i, c.to_i, d.to_i
    fullMul * ai * ci + halfMul * (ai*di + bi*ci) + bi * di

end

a, b, c, d = $*

n = a.length

mul = 10**n

ab = a.to_i*mul + b.to_i
cd = c.to_i*mul + d.to_i

puts "#{ab} x #{cd} = #{ab*cd}"

puts "Karatsuba: #{karaMul(a, b, c, d)}"
